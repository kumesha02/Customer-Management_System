/**
 * 🎓 LESSON: Complex Form with React Hook Form & Dynamic Fields
 * ============================================================================
 * 
 * This component demonstrates:
 * - React Hook Form for form management
 * - Dynamic field arrays (mobile numbers, addresses)
 * - Date picker integration
 * - Form validation
 * - Create vs Edit mode
 * - Route parameters
 * 
 * ============================================================================
 */

import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useForm, useFieldArray } from 'react-hook-form';
import DatePicker from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css';
import { customerService, masterDataService } from '../services/api';

function CustomerForm({ mode }) {
    const { id } = useParams(); // Get customer ID from URL (for edit mode)
    const navigate = useNavigate();

    /**
     * 🎓 REACT HOOK FORM: Powerful Form Management
     * ==============================================
     * 
     * React Hook Form makes forms easier:
     * - Less boilerplate code
     * - Built-in validation
     * - Better performance (less re-renders)
     * - Easy error handling
     * 
     * useForm() returns:
     * - register: Register input fields
     * - handleSubmit: Form submission handler
     * - formState: Form state (errors, isDirty, etc.)
     * - control: For advanced features (field arrays)
     * - setValue: Programmatically set field values
     * - reset: Reset form to default values
     */
    const {
        register,
        handleSubmit,
        control,
        setValue,
        watch,
        reset,
        formState: { errors, isSubmitting }
    } = useForm({
        defaultValues: {
            name: '',
            dateOfBirth: '',
            nicNumber: '',
            mobileNumbers: [{ number: '' }],
            addresses: [{ addressLine1: '', addressLine2: '', cityId: '' }],
            familyMemberIds: []
        }
    });

    /**
     * 🎓 useFieldArray: Dynamic Form Fields
     * =======================================
     * 
     * Manages arrays of fields (add/remove dynamically).
     * 
     * Perfect for:
     * - Multiple phone numbers
     * - Multiple addresses
     * - List of items
     * 
     * Returns:
     * - fields: Array of field objects
     * - append: Add new field
     * - remove: Remove field by index
     */
    const {
        fields: mobileFields,
        append: appendMobile,
        remove: removeMobile
    } = useFieldArray({
        control,
        name: 'mobileNumbers'
    });

    const {
        fields: addressFields,
        append: appendAddress,
        remove: removeAddress
    } = useFieldArray({
        control,
        name: 'addresses'
    });

    // Component state
    const [loading, setLoading] = useState(false);
    const [cities, setCities] = useState([]);
    const [allCustomers, setAllCustomers] = useState([]);
    const [selectedDate, setSelectedDate] = useState(null);

    /**
     * Load master data and customer (if edit mode)
     */
    useEffect(() => {
        loadMasterData();

        if (mode === 'edit' && id) {
            loadCustomer();
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [id, mode]);

    /**
     * Load cities and customers for dropdowns
     */
    const loadMasterData = async () => {
        try {
            // Load cities
            const citiesData = await masterDataService.getAllCities();
            setCities(citiesData);

            // Load all customers for family member selection
            const customersData = await customerService.getAllCustomers(0, 1000);
            setAllCustomers(customersData.content);
        } catch (err) {
            console.error('Error loading master data:', err);
            alert('Failed to load dropdown data');
        }
    };

    /**
     * Load customer data for editing
     */
    const loadCustomer = async () => {
        try {
            setLoading(true);
            const customer = await customerService.getCustomerById(id);

            // Populate form with customer data
            reset({
                name: customer.name,
                dateOfBirth: customer.dateOfBirth,
                nicNumber: customer.nicNumber,
                mobileNumbers: customer.mobileNumbers.length > 0
                    ? customer.mobileNumbers
                    : [{ number: '' }],
                addresses: customer.addresses.length > 0
                    ? customer.addresses
                    : [{ addressLine1: '', addressLine2: '', cityId: '' }],
                familyMemberIds: customer.familyMemberIds || []
            });

            // Set date picker value
            if (customer.dateOfBirth) {
                setSelectedDate(new Date(customer.dateOfBirth));
            }

        } catch (err) {
            console.error('Error loading customer:', err);
            alert('Failed to load customer data');
            navigate('/');
        } finally {
            setLoading(false);
        }
    };

    /**
     * Handle form submission
     */
    const onSubmit = async (data) => {
        try {
            // Prepare customer data
            const customerData = {
                ...data,
                // Filter out empty mobile numbers
                mobileNumbers: data.mobileNumbers.filter(m => m.number.trim() !== ''),
                // Filter out empty addresses
                addresses: data.addresses.filter(a =>
                    a.addressLine1.trim() !== '' || a.addressLine2.trim() !== '' || a.cityId
                )
            };

            if (mode === 'edit') {
                // Update existing customer
                await customerService.updateCustomer(id, customerData);
                alert('Customer updated successfully!');
            } else {
                // Create new customer
                await customerService.createCustomer(customerData);
                alert('Customer created successfully!');
            }

            // Navigate back to list
            navigate('/');

        } catch (err) {
            console.error('Error saving customer:', err);

            // Show specific error message
            if (err.response && err.response.data) {
                alert(`Error: ${err.response.data.message || 'Failed to save customer'}`);
            } else {
                alert('Failed to save customer. Please try again.');
            }
        }
    };

    /**
     * Handle date change from date picker
     */
    const handleDateChange = (date) => {
        setSelectedDate(date);
        if (date) {
            // Format date as YYYY-MM-DD for backend
            const formattedDate = date.toISOString().split('T')[0];
            setValue('dateOfBirth', formattedDate);
        } else {
            setValue('dateOfBirth', '');
        }
    };

    if (loading) {
        return (
            <div className="text-center mt-5">
                <div className="spinner-border text-primary" role="status">
                    <span className="visually-hidden">Loading...</span>
                </div>
            </div>
        );
    }

    return (
        <div className="container">
            <div className="row justify-content-center">
                <div className="col-lg-10">
                    <div className="card shadow">
                        <div className="card-header bg-primary text-white">
                            <h4 className="mb-0">
                                <i className={`bi ${mode === 'edit' ? 'bi-pencil' : 'bi-person-plus'} me-2`}></i>
                                {mode === 'edit' ? 'Edit Customer' : 'Add New Customer'}
                            </h4>
                        </div>
                        <div className="card-body p-4">
                            {/**
               * 🎓 FORM HANDLING with React Hook Form
               * =======================================
               * 
               * handleSubmit wraps our onSubmit function:
               * - Validates all fields
               * - Prevents submission if errors
               * - Calls onSubmit with validated data
               */}
                            <form onSubmit={handleSubmit(onSubmit)}>
                                {/* ========== BASIC INFORMATION ========== */}
                                <h5 className="border-bottom pb-2 mb-3">
                                    <i className="bi bi-person-badge me-2"></i>
                                    Basic Information
                                </h5>

                                <div className="row mb-3">
                                    {/* Name Field */}
                                    <div className="col-md-6">
                                        <label className="form-label">
                                            Name <span className="text-danger">*</span>
                                        </label>
                                        <input
                                            type="text"
                                            className={`form-control ${errors.name ? 'is-invalid' : ''}`}
                                            {...register('name', {
                                                required: 'Name is required',
                                                minLength: {
                                                    value: 2,
                                                    message: 'Name must be at least 2 characters'
                                                }
                                            })}
                                            placeholder="Enter full name"
                                        />
                                        {errors.name && (
                                            <div className="invalid-feedback">{errors.name.message}</div>
                                        )}
                                    </div>

                                    {/* NIC Number Field */}
                                    <div className="col-md-6">
                                        <label className="form-label">
                                            NIC Number <span className="text-danger">*</span>
                                        </label>
                                        <input
                                            type="text"
                                            className={`form-control ${errors.nicNumber ? 'is-invalid' : ''}`}
                                            {...register('nicNumber', {
                                                required: 'NIC number is required',
                                                pattern: {
                                                    value: /^[0-9]{9}[vVxX]$|^[0-9]{12}$/,
                                                    message: 'Invalid NIC format (e.g., 123456789V or 199012345678)'
                                                }
                                            })}
                                            placeholder="123456789V or 199012345678"
                                        />
                                        {errors.nicNumber && (
                                            <div className="invalid-feedback">{errors.nicNumber.message}</div>
                                        )}
                                    </div>
                                </div>

                                {/* Date of Birth */}
                                <div className="row mb-4">
                                    <div className="col-md-6">
                                        <label className="form-label">
                                            Date of Birth <span className="text-danger">*</span>
                                        </label>
                                        <DatePicker
                                            selected={selectedDate}
                                            onChange={handleDateChange}
                                            dateFormat="yyyy-MM-dd"
                                            className={`form-control ${errors.dateOfBirth ? 'is-invalid' : ''}`}
                                            placeholderText="Select date of birth"
                                            showYearDropdown
                                            scrollableYearDropdown
                                            yearDropdownItemNumber={100}
                                            maxDate={new Date()}
                                        />
                                        <input
                                            type="hidden"
                                            {...register('dateOfBirth', {
                                                required: 'Date of birth is required'
                                            })}
                                        />
                                        {errors.dateOfBirth && (
                                            <div className="text-danger small mt-1">
                                                {errors.dateOfBirth.message}
                                            </div>
                                        )}
                                    </div>
                                </div>

                                {/* ========== MOBILE NUMBERS ========== */}
                                <h5 className="border-bottom pb-2 mb-3 mt-4">
                                    <i className="bi bi-telephone me-2"></i>
                                    Mobile Numbers
                                </h5>

                                {mobileFields.map((field, index) => (
                                    <div key={field.id} className="row mb-2">
                                        <div className="col-md-8">
                                            <input
                                                type="text"
                                                className={`form-control ${errors.mobileNumbers?.[index]?.number ? 'is-invalid' : ''}`}
                                                {...register(`mobileNumbers.${index}.number`, {
                                                    pattern: {
                                                        value: /^[+]?[0-9]{10,15}$/,
                                                        message: 'Invalid phone number format'
                                                    }
                                                })}
                                                placeholder="+94771234567"
                                            />
                                            {errors.mobileNumbers?.[index]?.number && (
                                                <div className="invalid-feedback">
                                                    {errors.mobileNumbers[index].number.message}
                                                </div>
                                            )}
                                        </div>
                                        <div className="col-md-4">
                                            {index > 0 && (
                                                <button
                                                    type="button"
                                                    className="btn btn-outline-danger"
                                                    onClick={() => removeMobile(index)}
                                                >
                                                    <i className="bi bi-trash"></i> Remove
                                                </button>
                                            )}
                                        </div>
                                    </div>
                                ))}

                                <button
                                    type="button"
                                    className="btn btn-outline-primary btn-sm mt-2"
                                    onClick={() => appendMobile({ number: '' })}
                                >
                                    <i className="bi bi-plus-circle me-1"></i>
                                    Add Mobile Number
                                </button>

                                {/* ========== ADDRESSES ========== */}
                                <h5 className="border-bottom pb-2 mb-3 mt-4">
                                    <i className="bi bi-geo-alt me-2"></i>
                                    Addresses
                                </h5>

                                {addressFields.map((field, index) => (
                                    <div key={field.id} className="card mb-3">
                                        <div className="card-body">
                                            <div className="d-flex justify-content-between align-items-center mb-3">
                                                <h6 className="mb-0">Address {index + 1}</h6>
                                                {index > 0 && (
                                                    <button
                                                        type="button"
                                                        className="btn btn-outline-danger btn-sm"
                                                        onClick={() => removeAddress(index)}
                                                    >
                                                        <i className="bi bi-trash"></i> Remove
                                                    </button>
                                                )}
                                            </div>

                                            <div className="row mb-2">
                                                <div className="col-md-6">
                                                    <label className="form-label small">Address Line 1</label>
                                                    <input
                                                        type="text"
                                                        className="form-control"
                                                        {...register(`addresses.${index}.addressLine1`)}
                                                        placeholder="Street address"
                                                    />
                                                </div>
                                                <div className="col-md-6">
                                                    <label className="form-label small">Address Line 2</label>
                                                    <input
                                                        type="text"
                                                        className="form-control"
                                                        {...register(`addresses.${index}.addressLine2`)}
                                                        placeholder="Apartment, suite, etc."
                                                    />
                                                </div>
                                            </div>

                                            <div className="row">
                                                <div className="col-md-6">
                                                    <label className="form-label small">City</label>
                                                    <select
                                                        className="form-select"
                                                        {...register(`addresses.${index}.cityId`)}
                                                    >
                                                        <option value="">Select city...</option>
                                                        {cities.map(city => (
                                                            <option key={city.id} value={city.id}>
                                                                {city.name} ({city.country.name})
                                                            </option>
                                                        ))}
                                                    </select>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                ))}

                                <button
                                    type="button"
                                    className="btn btn-outline-primary btn-sm mt-2"
                                    onClick={() => appendAddress({ addressLine1: '', addressLine2: '', cityId: '' })}
                                >
                                    <i className="bi bi-plus-circle me-1"></i>
                                    Add Address
                                </button>

                                {/* ========== FAMILY MEMBERS ========== */}
                                <h5 className="border-bottom pb-2 mb-3 mt-4">
                                    <i className="bi bi-people me-2"></i>
                                    Family Members
                                </h5>

                                <div className="mb-3">
                                    <label className="form-label">Select Family Members</label>
                                    <select
                                        multiple
                                        className="form-select"
                                        size="5"
                                        {...register('familyMemberIds')}
                                    >
                                        {allCustomers
                                            .filter(c => c.id !== parseInt(id)) // Exclude current customer
                                            .map(customer => (
                                                <option key={customer.id} value={customer.id}>
                                                    {customer.name} ({customer.nicNumber})
                                                </option>
                                            ))}
                                    </select>
                                    <div className="form-text">
                                        Hold Ctrl (Cmd on Mac) to select multiple family members
                                    </div>
                                </div>

                                {/* ========== FORM ACTIONS ========== */}
                                <div className="d-flex justify-content-between mt-4 pt-3 border-top">
                                    <button
                                        type="button"
                                        className="btn btn-secondary"
                                        onClick={() => navigate('/')}
                                    >
                                        <i className="bi bi-x-circle me-2"></i>
                                        Cancel
                                    </button>
                                    <button
                                        type="submit"
                                        className="btn btn-primary"
                                        disabled={isSubmitting}
                                    >
                                        {isSubmitting ? (
                                            <>
                                                <span className="spinner-border spinner-border-sm me-2" role="status"></span>
                                                Saving...
                                            </>
                                        ) : (
                                            <>
                                                <i className="bi bi-check-circle me-2"></i>
                                                {mode === 'edit' ? 'Update Customer' : 'Create Customer'}
                                            </>
                                        )}
                                    </button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default CustomerForm;

/**
 * 🎓 KEY CONCEPTS DEMONSTRATED:
 * ==============================
 * 
 * 1. REACT HOOK FORM:
 *    - useForm hook for form state
 *    - register() for field registration
 *    - Validation rules
 *    - Error handling
 * 
 * 2. DYNAMIC FIELDS:
 *    - useFieldArray for arrays
 *    - append() to add fields
 *    - remove() to delete fields
 * 
 * 3. DATE PICKER:
 *    - react-datepicker integration
 *    - Date formatting
 *    - Year dropdown for easy selection
 * 
 * 4. ROUTE PARAMETERS:
 *    - useParams() to get URL params
 *    - Different behavior for create/edit
 * 
 * 5. FORM VALIDATION:
 *    - Required fields
 *    - Pattern matching (regex)
 *    - Min/max length
 *    - Custom error messages
 * 
 * 6. CONTROLLED COMPONENTS:
 *    - Form values controlled by React Hook Form
 *    - setValue() for programmatic updates
 * 
 * 7. LOADING STATES:
 *    - Disable submit during submission
 *    - Show spinner
 *    - Prevent double submission
 * 
 * ============================================================================
 */
